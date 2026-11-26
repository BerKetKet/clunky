#!/usr/bin/env bash
set -euo pipefail

PKG="${1:-com.azath.reader}"
RUNS="${2:-5}"
DURATION_SEC="${3:-120}"
OUT_DIR="${4:-/tmp/diagnose_native_crash/${PKG//./_}_$(date +%Y%m%d_%H%M%S)}"

mkdir -p "${OUT_DIR}"

resolve_launcher() {
    local pkg="$1"
    adb wait-for-device >/dev/null 2>&1 || true
    adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER "${pkg}" 2>/dev/null | tail -n1 | tr -d '\r'
}

check_pid_state() {
    local pkg="$1"
    local expect_pid="$2"
    local raw
    raw="$(adb shell "pidof ${pkg} 2>/dev/null; echo __PROBE_OK__" 2>/dev/null | tr -d '\r' || true)"
    if [[ "${raw}" != *"__PROBE_OK__"* ]]; then
        echo "ADB_BLIP"
        return 0
    fi
    local pids
    pids="$(printf "%s\n" "${raw}" | grep -v '__PROBE_OK__' | tr '\n' ' ' | xargs || true)"
    if [[ -z "${pids}" ]]; then
        echo "GONE"
        return 0
    fi
    local first_pid
    first_pid="$(printf "%s" "${pids}" | awk '{print $1}')"
    for p in ${pids}; do
        if [[ "${p}" == "${expect_pid}" ]]; then
            echo "ALIVE:${p}"
            return 0
        fi
    done
    echo "CHANGED:${first_pid}"
}

LAUNCHER_ACT="$(resolve_launcher "${PKG}")"
if [[ -z "${LAUNCHER_ACT}" || "${LAUNCHER_ACT}" == "No activity found" ]]; then
    echo "ERROR: Could not resolve LAUNCHER activity for ${PKG}" >&2
    exit 1
fi

echo "=== Native Crash Diagnostic Harness ==="
echo "Package:  ${PKG} (${LAUNCHER_ACT})"
echo "Runs:     ${RUNS} x ${DURATION_SEC}s idle"
echo "OutDir:   ${OUT_DIR}"
echo ""

SUMMARY_TSV="${OUT_DIR}/summary.tsv"
printf "run\tpkg\tpids\tuptime_sec\tstatus\tfatal_signals\n" > "${SUMMARY_TSV}"

for ((r=1; r<=RUNS; r++)); do
    LOG_FILE="${OUT_DIR}/run_${r}.log"
    adb wait-for-device >/dev/null 2>&1 || true
    adb shell am force-stop "${PKG}" >/dev/null 2>&1 || true
    sleep 1
    adb logcat -c

    T0_MS="$(date +%s%3N)"
    adb shell am start -W -n "${LAUNCHER_ACT}" >/dev/null 2>&1
    PID_INIT=""
    for ((w=0; w<15; w++)); do
        PID_INIT="$(adb shell pidof "${PKG}" 2>/dev/null | tr -d '\r' | awk '{print $1}' || true)"
        if [[ -n "${PID_INIT}" ]]; then
            break
        fi
        sleep 0.2
    done

    STATUS="UNKNOWN"
    UPTIME_SEC=0
    CURRENT_PIDS="${PID_INIT}"

    if [[ -z "${PID_INIT}" ]]; then
        STATUS="NO_PID_ON_LAUNCH"
    else
        DIED=0
        for ((s=1; s<=DURATION_SEC; s++)); do
            sleep 1
            STATE="$(check_pid_state "${PKG}" "${PID_INIT}")"
            if [[ "${STATE}" == "ADB_BLIP" ]]; then
                adb wait-for-device >/dev/null 2>&1 || true
                sleep 0.2
                STATE="$(check_pid_state "${PKG}" "${PID_INIT}")"
            fi
            UPTIME_MS=$(( $(date +%s%3N) - T0_MS ))
            UPTIME_SEC="$(awk "BEGIN {printf \"%.1f\", ${UPTIME_MS}/1000.0}")"
            if [[ "${STATE}" == "GONE" ]]; then
                sleep 0.2
                STATE2="$(check_pid_state "${PKG}" "${PID_INIT}")"
                if [[ "${STATE2}" == "GONE" ]]; then
                    STATUS="DIED"
                    DIED=1
                    break
                fi
            elif [[ "${STATE}" == CHANGED:* ]]; then
                NEW_PID="${STATE#CHANGED:}"
                CURRENT_PIDS="${PID_INIT}->${NEW_PID}"
                STATUS="RESPAWNED"
                DIED=1
                break
            fi
        done
        if [[ "${DIED}" -eq 0 ]]; then
            STATUS="SURVIVED_${DURATION_SEC}s"
            UPTIME_SEC="${DURATION_SEC}.0"
        fi
    fi

    adb wait-for-device >/dev/null 2>&1 || true
    adb logcat -d -b crash,main > "${LOG_FILE}" || true
    SIGNALS="$(grep -E 'Fatal signal|SIGSYS|SIGILL|SIGSEGV|SIGABRT|FATAL EXCEPTION|libKindleAndroidNativeBundlerJNI' "${LOG_FILE}" | head -n 5 | tr '\n' ' | ' | tr '\t' ' ' || true)"
    if [[ -z "${SIGNALS}" ]]; then
        SIGNALS="none"
    fi

    printf "%d\t%s\t%s\t%s\t%s\t%s\n" "${r}" "${PKG}" "${CURRENT_PIDS:-none}" "${UPTIME_SEC}" "${STATUS}" "${SIGNALS}" >> "${SUMMARY_TSV}"
    printf "[Run %d/%d] pkg=%s pid=%s uptime=%ss status=%s\n  signals: %s\n" "${r}" "${RUNS}" "${PKG}" "${CURRENT_PIDS:-none}" "${UPTIME_SEC}" "${STATUS}" "${SIGNALS}"
    adb shell am force-stop "${PKG}" >/dev/null 2>&1 || true
done

echo ""
echo "Summary written to ${SUMMARY_TSV}"
cat "${SUMMARY_TSV}"

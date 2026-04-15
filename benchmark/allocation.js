import http from 'k6/http';
import {check, sleep} from 'k6';
import {Trend} from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DEBUG = __ENV.DEBUG || false;
const POLL_INTERVAL_S = 0.05;

const allocationLatency = new Trend('allocation_e2e_ms', true);

export const options = {
  vus: 5,
  iterations: 50,
  summaryTrendStats: ['min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

const HEADERS = { 'Content-Type': 'application/json' };

export default function () {
  const start = Date.now();

  const createRes = http.post(`${BASE_URL}/allocations`, JSON.stringify({
    employeeId: crypto.randomUUID(),
    policy: [
        { equipmentType: 'main_computer' },
        { equipmentType: 'monitor' },
    ],
  }), { headers: HEADERS });

  check(createRes, { 'POST /allocations → 202': (r) => r.status === 202 });

  const allocationId = createRes.json('allocationId');
  if (!allocationId) return;

  let state = 'pending';
  while (state === 'pending') {
    sleep(POLL_INTERVAL_S);
    const statusRes = http.get(`${BASE_URL}/allocations/${allocationId}`);
    if (DEBUG) {
      console.log(statusRes.json());
    }
    if (statusRes.status === 200) {
      state = statusRes.json('state');
    }
  }

  allocationLatency.add(Date.now() - start);
}
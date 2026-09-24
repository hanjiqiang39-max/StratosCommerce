import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    browse: {
      executor: 'constant-vus',
      vus: 5,
      duration: '30s',
      exec: 'browse',
    },
    seckill: {
      executor: 'constant-arrival-rate',
      rate: 20,
      timeUnit: '1s',
      duration: '20s',
      preAllocatedVUs: 20,
      exec: 'seckill',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.2'],
  },
};

export function browse() {
  const list = http.get(`${BASE}/api/product/list?pageNum=1&pageSize=10`);
  check(list, { 'product list 200': (r) => r.status === 200 });
  sleep(0.5);
}

export function seckill() {
  const res = http.post(
    `${BASE}/api/seckill/do`,
    JSON.stringify({ seckillId: 1, userId: 1, quantity: 1 }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { 'seckill responded': (r) => r.status === 200 || r.status === 401 || r.status === 429 });
}

export function setup() {
  const suffix = `${Date.now()}`.slice(-6);
  const username = `k6u${suffix}`;
  const password = 'Test1234';
  http.post(
    `${BASE}/api/user/register`,
    JSON.stringify({ username, password, nickname: 'k6' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const login = http.post(
    `${BASE}/api/user/login`,
    JSON.stringify({ username, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const body = login.json();
  const token = body && body.data ? body.data.token : '';
  const userId = body && body.data ? body.data.userId : 0;
  if (token && userId) {
    http.post(
      `${BASE}/api/order/create`,
      JSON.stringify({
        userId,
        items: [{ skuId: 1000101, quantity: 1 }],
        receiverName: 'k6',
        receiverPhone: '13800000000',
        receiverProvince: '广东',
        receiverCity: '深圳',
        receiverDistrict: '南山',
        receiverDetailAddress: '测试路1号',
      }),
      { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
    );
  }
  return { token, userId };
}

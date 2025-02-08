const BASE_URL = 'https://api.com';

/**
 * @typedef {Object} RequestOptions
 * @property {string} method - HTTP 메서드 (GET, POST, PUT, PATCH, DELETE)
 * @property {Object.<string, string>} headers - 요청 헤더
 * @property {string} [body] - 요청 바디 (JSON 문자열)
 */

/**
 * API 요청을 보내는 함수
 * @template T
 * @param {string} method - HTTP 메서드 (GET, POST, PUT, PATCH, DELETE)
 * @param {string} url - API URL
 * @param {Object} [body] - 요청 바디 (옵션)
 * @param {Object} [params] - URL 파라미터 (옵션)
 * @returns {Promise<T>} - JSON 파싱된 응답 데이터
 */
const request = async (method, url, body, params = {}) => {
  /** @type {RequestOptions} */
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  // URL에 파라미터 추가
  const queryString = new URLSearchParams(params).toString();
  const fullUrl =
    queryString ? `${BASE_URL}${url}?${queryString}` : `${BASE_URL}${url}`;

  console.log(queryString, fullUrl);
  try {
    const response = await fetch(fullUrl, options);
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    return response.json();
  } catch (error) {
    console.error('API Request Error:', error);
    throw error;
  }
};

/**
 * API 모듈
 */
export const api = {
  /**
   * GET 요청
   * @template T
   * @param {string} url - API URL
   * @param {Object} [params] - URL 파라미터
   * @returns {Promise<T>}
   */
  get: (url, params) => request('GET', url, params),

  /**
   * POST 요청
   * @template T
   * @param {string} url - API URL
   * @param {Object} body - 요청 바디
   * @param {Object} [params] - URL 파라미터
   * @returns {Promise<T>}
   */
  post: (url, body, params) => request('POST', url, body, params),

  /**
   * PUT 요청
   * @template T
   * @param {string} url - API URL
   * @param {Object} body - 요청 바디
   * @param {Object} [params] - URL 파라미터
   *
   * @returns {Promise<T>}
   */
  put: (url, body, params) => request('PUT', url, body, params),

  /**
   * PATCH 요청
   * @template T
   * @param {string} url - API URL
   * @param {Object} body - 요청 바디
   * @param {Object} [params] - URL 파라미터
   * @returns {Promise<T>}
   */
  patch: (url, body, params) => request('PATCH', url, body, params),

  /**
   * DELETE 요청
   * @template T
   * @param {string} url - API URL
   * @param {Object} [params] - URL 파라미터
   * @returns {Promise<T>}
   */
  delete: (url, params) => request('DELETE', url, params),
};

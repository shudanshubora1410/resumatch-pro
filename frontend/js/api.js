/** API Client — auto-detects base URL from browser location */
(function() {
  var origin = window.location.origin;
  // If frontend is on a dev server port, point API to Spring Boot
  if (origin.indexOf(':3000') !== -1 || origin.indexOf(':5500') !== -1 || origin.indexOf(':80') === -1 && origin.indexOf(':8080') === -1) {
    origin = 'http://localhost:8080';
  }
  window.API_BASE = origin + '/api';
})();

var Api = {
    getToken: function() { return localStorage.getItem('access_token'); },
    getRefreshToken: function() { return localStorage.getItem('refresh_token'); },

    async request(endpoint, options) {
        options = options || {};
        var token = this.getToken();
        var headers = { 'Content-Type': 'application/json' };
        if (options.headers) Object.assign(headers, options.headers);
        if (token) headers['Authorization'] = 'Bearer ' + token;
        if (options.body instanceof FormData) delete headers['Content-Type'];

        try {
            var r = await fetch(window.API_BASE + endpoint, Object.assign({}, options, { headers: headers }));
            if (r.status === 401 && this.getRefreshToken()) {
                var ref = await this.refreshToken();
                if (ref) {
                    headers['Authorization'] = 'Bearer ' + this.getToken();
                    r = await fetch(window.API_BASE + endpoint, Object.assign({}, options, { headers: headers }));
                }
            }
            return r;
        } catch (err) { throw err; }
    },

    async refreshToken() {
        var rt = this.getRefreshToken(); if (!rt) return false;
        try {
            var r = await fetch(window.API_BASE + '/auth/refresh', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken: rt }) });
            if (r.ok) { var d = await r.json(); if (d.success && d.data) { Utils.saveTokens(d.data.accessToken, d.data.refreshToken); return true; } }
            Utils.clearTokens(); window.location.href = '/login.html'; return false;
        } catch (e) { return false; }
    },

    async get(e) { var r = await this.request(e, { method: 'GET' }); return r.json(); },
    async post(e, d, f) { var r = await this.request(e, { method: 'POST', body: f ? d : JSON.stringify(d) }); return r.json(); },
    async put(e, d) { var r = await this.request(e, { method: 'PUT', body: JSON.stringify(d) }); return r.json(); },
    async delete(e) { var r = await this.request(e, { method: 'DELETE' }); return r.json(); },
    async upload(e, d) { var r = await this.request(e, { method: 'POST', body: d }); return r.json(); }
};

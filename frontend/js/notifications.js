/**
 * Notification Bell + Dropdown
 * Polls unread count every 30s
 */
var Notifications = {
    pollInterval: null,
    init: function(bellId, badgeId, dropdownId) {
        this.bellEl = document.getElementById(bellId);
        this.badgeEl = document.getElementById(badgeId);
        this.dropdownEl = document.getElementById(dropdownId);
        if (!this.bellEl) return;
        this.fetchUnreadCount();
        this.pollInterval = setInterval(this.fetchUnreadCount.bind(this), 30000);
    },
    fetchUnreadCount: function() {
        var self = this;
        if (!Utils.isLoggedIn()) return;
        Api.get('/notifications/unread-count').then(function(res) {
            if (res.success && self.badgeEl) {
                var count = res.data || 0;
                self.badgeEl.textContent = count;
                self.badgeEl.classList.toggle('d-none', count === 0);
            }
        }).catch(function() {});
    },
    loadDropdown: function() {
        var self = this;
        if (!this.dropdownEl) return;
        Api.get('/notifications?page=0&size=5').then(function(res) {
            if (!res.success) return;
            var items = res.data.content || [];
            var html = '';
            if (items.length === 0) {
                html = '<div class="p-3 text-center text-muted small">No notifications</div>';
            } else {
                items.forEach(function(n) {
                    html += '<a href="' + (n.redirectUrl || '#') + '" class="dropdown-item ' + (n.isRead ? '' : 'fw-bold') + '"><small>' + n.title + '</small><br><small class="text-muted">' + Utils.truncate(n.message, 60) + '</small></a>';
                });
                html += '<div class="dropdown-divider"></div><a class="dropdown-item text-center small text-primary" href="#">View All</a>';
            }
            self.dropdownEl.innerHTML = html;
        }).catch(function() {});
    },
    stop: function() {
        if (this.pollInterval) clearInterval(this.pollInterval);
    }
};

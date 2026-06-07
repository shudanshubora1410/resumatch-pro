/** Seeker Dashboard - stats + recent apps + score trend */
var SeekerDashboard = {
    init: function() {
        Utils.requireAuth();
        var user = Utils.getUser();
        if (user) {
            document.getElementById('welcomeName').textContent = user.fullName || 'User';
            document.getElementById('userNameDisplay').textContent = user.fullName || 'User';
            if (user.role !== 'JOB_SEEKER') Auth.redirectToDashboard(user.role);
        }
        this.loadStats();
    },
    loadStats: function() {
        Api.get('/seeker/dashboard/stats').then(function(r) {
            if (r.success) {
                var d = r.data;
                document.getElementById('statApps').textContent = d.applicationsSent || 0;
                document.getElementById('statScore').textContent = (d.averageAtsScore || 0) + '%';
                document.getElementById('statShort').textContent = d.shortlistedCount || 0;
                document.getElementById('statPending').textContent = d.pendingCount || 0;
            }
        }).catch(function() {});
    }
};

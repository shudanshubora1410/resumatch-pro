/** Manage Job Listings */
var ManageJobs = {
    currentFilter: 'ALL',
    jobs: [],
    init: function() { Utils.requireAuth(); this.load(); },
    load: function() {
        Api.get('/recruiter/jobs?page=0&size=50').then(function(r) {
            if (r.success) { ManageJobs.jobs = r.data.content || []; ManageJobs.render(); }
        }).catch(function(){});
    },
    filter: function(status) {
        this.currentFilter = status;
        document.querySelectorAll('#statusTabs button').forEach(function(b) { b.classList.remove('active'); });
        event.target.classList.add('active');
        this.render();
    },
    render: function() {
        var filtered = this.currentFilter === 'ALL' ? this.jobs : this.jobs.filter(function(j) { return j.status === this.currentFilter; }.bind(this));
        var html = '';
        if (!filtered.length) { html = '<div class="text-center py-5"><p class="text-muted">No jobs found</p><a href="post-job.html" class="btn btn-primary btn-sm">Post a Job</a></div>'; }
        filtered.forEach(function(j) {
            var statusCls = j.status === 'ACTIVE' ? 'status-shortlisted' : j.status === 'DRAFT' ? 'status-applied' : 'status-rejected';
            html += '<div class="job-row"><div class="row align-items-center"><div class="col-md-3"><h6 class="fw-bold mb-1">'+j.jobTitle+'</h6><span class="status-badge '+statusCls+'">'+j.status+'</span></div>'+
            '<div class="col-md-2 text-center"><div class="fw-bold">'+(j.applicantCount||0)+'</div><small class="text-muted">Apps</small></div>'+
            '<div class="col-md-2 text-center"><div class="fw-bold">'+(j.avgScore||0)+'</div><small class="text-muted">Avg Score</small></div>'+
            '<div class="col-md-2 text-center"><div class="fw-bold">'+(j.shortlistedCount||0)+'</div><small class="text-muted">Shortlisted</small></div>'+
            '<div class="col-md-3 text-end"><a href="applicants.html?jobId='+j.id+'" class="btn btn-sm btn-outline-primary">View</a> <a href="edit-job.html?id='+j.id+'" class="btn btn-sm btn-outline-secondary">Edit</a></div></div></div>';
        });
        document.getElementById('jobsContent').innerHTML = html;
    },
    closeJob: function(id) {
        if (confirm('Close this job?')) {
            Api.put('/recruiter/jobs/'+id+'/status', {status:'CLOSED'}).then(function(r) {
                if (r.success) { Toast.show('Job closed','warning'); ManageJobs.load(); }
            });
        }
    }
};

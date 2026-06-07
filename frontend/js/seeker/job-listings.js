/** Browse Jobs - search, filter, paginate */
var JobListings = {
    currentPage: 0,
    currentFilter: 'ALL',
    searchTimeout: null,
    init: function() {
        Utils.requireAuth();
        this.fetchJobs();
    },
    debounceSearch: function() {
        var self = this;
        clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(function() { self.fetchJobs(); }, 400);
    },
    fetchJobs: function() {
        var params = 'page=' + this.currentPage + '&size=10';
        var search = document.getElementById('searchInput').value.trim();
        var type = document.getElementById('typeFilter').value;
        var loc = document.getElementById('locationFilter').value.trim();
        var exp = document.getElementById('expFilter').value;
        if (search) params += '&search=' + encodeURIComponent(search);
        if (type) params += '&type=' + type;
        if (loc) params += '&location=' + encodeURIComponent(loc);
        if (exp) params += '&minExp=' + exp;
        document.getElementById('jobsContent').innerHTML = '<div class="text-center py-5"><div class="spinner-border text-primary mb-2"></div></div>';
        Api.get('/seeker/jobs?' + params).then(function(r) {
            if (r.success) JobListings.render(r.data.content, r.data);
        }).catch(function() {});
    },
    render: function(jobs, page) {
        var html = '';
        if (!jobs || !jobs.length) {
            html = '<div class="text-center py-5"><i class="fa-solid fa-search fa-3x text-muted mb-3 d-block"></i><p class="text-muted">No jobs found</p></div>';
        }
        jobs.forEach(function(j) {
            var scoreColor = j.matchPercentage >= 70 ? 'bg-success' : j.matchPercentage >= 50 ? 'bg-warning' : 'bg-secondary';
            html += '<a href="job-detail.html?id=' + j.id + '" class="text-decoration-none">' +
            '<div class="job-card"><div class="d-flex align-items-start gap-3">' +
            '<div class="company-logo">' + (j.companyName||'C').charAt(0) + '</div>' +
            '<div class="flex-grow-1"><div class="d-flex justify-content-between">' +
            '<h6 class="fw-bold mb-1">' + j.jobTitle + '</h6>' +
            (j.matchPercentage ? '<span class="match-pill">' + j.matchPercentage + '%</span>' : '') +
            '</div><p class="mb-1 small text-muted">' + j.companyName + ' · ' + (j.isRemote?'Remote':j.location) + '</p>' +
            '<div class="d-flex gap-1 mt-1">' + (j.requiredSkills||'').split(',').slice(0,3).map(function(s){return '<span class="badge bg-light text-dark me-1">'+s.trim()+'</span>'}).join('') + '</div>' +
            '<div class="d-flex justify-content-between mt-2"><span class="small text-muted">' + (j.salaryRange||'') + '</span>' +
            '<small class="text-muted"><i class="fa-solid fa-clock me-1"></i>' + (j.applicationDeadline||'N/A') + '</small></div>' +
            '</div></div></div></a>';
        });
        document.getElementById('jobsContent').innerHTML = html;
        Pagination.render('jobsPagination', this.currentPage, page.totalPages || 1, function(p) {
            JobListings.currentPage = p; JobListings.fetchJobs(); window.scrollTo({top:0,behavior:'smooth'});
        });
    }
};

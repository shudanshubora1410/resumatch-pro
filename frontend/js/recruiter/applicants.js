/** Applicant Management - score-ranked table with bulk actions */
var Applicants = {
    selectedIds: new Set(),
    applicants: [],
    init: function() {
        Utils.requireAuth();
        this.load();
    },
    load: function() {
        var jobId = new URLSearchParams(window.location.search).get('jobId') || '1';
        Api.get('/recruiter/jobs/'+jobId+'/applicants?page=0&size=50').then(function(r) {
            if (r.success) { Applicants.applicants = r.data.content || []; Applicants.render(); }
        }).catch(function(){});
    },
    render: function() {
        var filtered = this.applicants;
        var minS = parseInt(document.getElementById('minScore').value) || 0;
        var maxS = parseInt(document.getElementById('maxScore').value) || 100;
        var stF = document.getElementById('statusFilter').value;
        var search = document.getElementById('searchInput').value.toLowerCase();
        filtered = filtered.filter(function(a) {
            if (a.score < minS || a.score > maxS) return false;
            if (stF && a.status !== stF) return false;
            if (search && !(a.name||'').toLowerCase().includes(search) && !(a.email||'').toLowerCase().includes(search)) return false;
            return true;
        });
        filtered.sort(function(a,b) { return (b.score||0) - (a.score||0); });

        var html = '<div class="table-responsive"><table class="table table-hover"><thead class="table-light"><tr><th style="width:40px"><input type="checkbox" id="selectAll" onchange="Applicants.toggleSelectAll(this)"></th><th>#</th><th>Candidate</th><th>Score</th><th>Match</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead><tbody>';
        if (!filtered.length) { html += '<tr><td colspan="8" class="text-center text-muted py-4">No applicants</td></tr>'; }
        filtered.forEach(function(a, idx) {
            var sc = a.score || 0;
            var scColor = sc >= 70 ? '#10B981' : sc >= 50 ? '#F59E0B' : '#EF4444';
            var rankCls = idx === 0 ? 'gold' : idx === 1 ? 'silver' : idx === 2 ? 'bronze' : '';
            html += '<tr><td><input type="checkbox" class="app-check" value="'+a.id+'" onchange="Applicants.updateSelection()" '+(Applicants.selectedIds.has(a.id)?'checked':'')+'></td>'+
            '<td><span class="rank-badge '+rankCls+'">'+(idx+1)+'</span></td>'+
            '<td><div class="fw-medium">'+(a.name||'Unknown')+'</div><small class="text-muted">'+(a.email||'')+'</small></td>'+
            '<td><span style="font-weight:700;color:'+scColor+'">'+sc+'</span><span class="mini-bar ms-2"><span class="mini-bar-fill" style="width:'+sc+'%;background:'+scColor+'"></span></span></td>'+
            '<td><span class="badge '+(a.skillMatch >= 70 ? 'bg-success' : a.skillMatch >= 50 ? 'bg-warning text-dark' : 'bg-danger')+'">'+(a.skillMatch||0)+'%</span></td>'+
            '<td><span class="status-badge '+Utils.getStatusClass(a.status)+'">'+Utils.getStatusLabel(a.status)+'</span></td>'+
            '<td><small>'+Utils.formatDate(a.appliedAt)+'</small></td>'+
            '<td><div class="dropdown"><button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">Actions</button><ul class="dropdown-menu">'+
            '<li><a class="dropdown-item" href="#" onclick="Applicants.quickAction('+a.id+',\'SHORTLISTED\')">Star Shortlist</a></li>'+
            '<li><a class="dropdown-item" href="#" onclick="Applicants.quickAction('+a.id+',\'REJECTED\')">X Reject</a></li>'+
            '<li><hr class="dropdown-divider"></li>'+
            '<li><a class="dropdown-item" href="#">Schedule Interview</a></li>'+
            '<li><a class="dropdown-item" href="#">Download Resume</a></li></ul></div></td></tr>';
        });
        html += '</tbody></table></div>';
        document.getElementById('applicantsContent').innerHTML = html;
    },
    toggleSelectAll: function(cb) {
        var self = this;
        document.querySelectorAll('.app-check').forEach(function(c) {
            c.checked = cb.checked;
            if (cb.checked) self.selectedIds.add(parseInt(c.value));
            else self.selectedIds.delete(parseInt(c.value));
        });
        this.updateBulkBar();
    },
    updateSelection: function() {
        this.selectedIds.clear();
        document.querySelectorAll('.app-check:checked').forEach(function(c) { Applicants.selectedIds.add(parseInt(c.value)); });
        this.updateBulkBar();
    },
    updateBulkBar: function() {
        var bar = document.getElementById('bulkBar');
        document.getElementById('bulkCount').textContent = this.selectedIds.size + ' selected';
        bar.classList.toggle('show', this.selectedIds.size > 0);
    },
    deselectAll: function() {
        this.selectedIds.clear();
        document.querySelectorAll('.app-check').forEach(function(c) { c.checked = false; });
        document.getElementById('selectAll').checked = false;
        this.updateBulkBar();
    },
    quickAction: function(id, status) {
        Toast.show(status === 'SHORTLISTED' ? 'Shortlisted!' : 'Rejected!', status === 'SHORTLISTED' ? 'success' : 'warning');
    },
    bulkAction: function(status) {
        Toast.show(this.selectedIds.size + ' applicants ' + (status === 'SHORTLISTED' ? 'shortlisted!' : 'rejected!'), 'success');
        this.deselectAll();
    }
};

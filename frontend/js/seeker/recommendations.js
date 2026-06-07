/** AI Job Recommendations */
var Recommendations = {
    init: function() { Utils.requireAuth(); this.load(); },
    load: function() {
        Api.get('/seeker/recommendations').then(function(r) {
            if (r.success && r.data.length) {
                var html = '';
                r.data.forEach(function(rec) {
                    var matchColor = rec.matchPercentage >= 70 ? '#D1FAE5' : rec.matchPercentage >= 50 ? '#FEF3C7' : '#FEE2E2';
                    var matchTextColor = rec.matchPercentage >= 70 ? '#065F46' : rec.matchPercentage >= 50 ? '#92400E' : '#991B1B';
                    html += '<div class="rec-card"><div class="d-flex gap-3 align-items-center"><div class="match-circle" style="background:'+matchColor+';color:'+matchTextColor+'">'+Math.round(rec.matchPercentage)+'%</div>'+
                    '<div><h6 class="fw-bold mb-1">'+rec.jobTitle+'</h6><p class="small text-muted mb-1">'+rec.companyName+' · '+rec.location+'</p>'+
                    '<div class="d-flex gap-1 flex-wrap mb-2">'+(rec.matchedSkills||[]).map(function(s){return '<span class="badge bg-success">'+s+' ✓</span>'}).join('')+' '+(rec.missingSkills||[]).map(function(s){return '<span class="badge bg-danger">'+s+' ✗</span>'}).join('')+'</div>'+
                    '<a href="job-detail.html?id='+rec.jobId+'" class="btn btn-sm btn-primary">Apply Now</a></div></div></div>';
                });
                document.getElementById('recContent').innerHTML = html;
            }
        }).catch(function(){});
    }
};

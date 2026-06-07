/**
 * What-If ATS Score Simulator
 * Pure client-side simulation — no data saved to server.
 * User checks missing skills/keywords and sees projected score update in real-time.
 */
var WhatIfSimulator = {
    originalScore: 0,
    missingKeywords: [],
    missingSkills: [],
    scoreBreakdown: {},

    init: function(config) {
        this.originalScore = config.originalScore || 0;
        this.missingKeywords = config.missingKeywords || [];
        this.missingSkills = config.missingSkills || [];
        this.scoreBreakdown = config.scoreBreakdown || {};
        this.renderSimulator('whatIfContainer');
    },

    renderSimulator: function(containerId) {
        var container = document.getElementById(containerId);
        if (!container) return;

        var allItems = [];
        var self = this;
        this.missingKeywords.forEach(function(k) { allItems.push({ type: 'keyword', label: k, gain: 2 }); });
        this.missingSkills.forEach(function(s) { allItems.push({ type: 'skill', label: s, gain: 1.5 }); });

        if (allItems.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3"><i class="fa-solid fa-circle-check text-success fa-2x mb-2 d-block"></i>All keywords and skills matched! No simulation needed.</div>';
            return;
        }

        var html = '<div class="card"><div class="card-header bg-transparent"><h6 class="fw-bold mb-0"><i class="fa-solid fa-flask me-2"></i>What-If Score Simulator</h6></div><div class="card-body">';
        html += '<p class="text-muted small mb-3">Check the skills/keywords you plan to add to see how your score would improve:</p>';

        html += '<div class="row mb-3"><div class="col-md-8"><div class="simulator-checklist" style="max-height:250px;overflow-y:auto;">';
        allItems.forEach(function(item, idx) {
            html += '<div class="form-check mb-1"><input class="form-check-input sim-check" type="checkbox" id="sim' + idx + '" data-gain="' + item.gain + '" data-label="' + item.label + '"><label class="form-check-label small" for="sim' + idx + '">' + item.label + ' <span class="badge ' + (item.type === 'keyword' ? 'bg-warning' : 'bg-info') + ' text-dark">+' + item.gain + ' pts</span></label></div>';
        });
        html += '</div></div>';
        html += '<div class="col-md-4 text-center border-start"><div class="mb-2"><small class="text-muted">Current Score</small><h3 class="fw-bold text-' + self.getScoreColor(self.originalScore) + '">' + self.originalScore + '</h3></div><div><small class="text-muted">Projected Score</small><h3 class="fw-bold text-primary" id="projectedScore">' + self.originalScore + '</h3></div><div id="projectedGrade" class="badge bg-primary">' + self.getGradeLabel(self.originalScore).grade + '</div></div></div>';

        html += '<div class="progress mt-2" style="height:8px;"><div class="progress-bar bg-success" id="simProgressBar" style="width:' + (self.originalScore) + '%"></div></div>';
        html += '<p class="text-muted small mt-2 mb-0"><i class="fa-solid fa-circle-info me-1"></i>This is a simulation. Upload an updated resume to get your real score.</p>';
        html += '</div></div>';

        container.innerHTML = html;

        // Attach event listeners
        var checkboxes = container.querySelectorAll('.sim-check');
        checkboxes.forEach(function(cb) {
            cb.addEventListener('change', function() { self.recalculate(); });
        });

        // "Select All" button
        var selectAllBtn = document.createElement('button');
        selectAllBtn.className = 'btn btn-sm btn-outline-secondary mt-2';
        selectAllBtn.textContent = 'Select All Missing';
        selectAllBtn.addEventListener('click', function() {
            var allChecked = true;
            checkboxes.forEach(function(c) { if (!c.checked) allChecked = false; });
            checkboxes.forEach(function(c) { c.checked = !allChecked; });
            self.recalculate();
        });
        container.querySelector('.simulator-checklist').after(selectAllBtn);
    },

    recalculate: function() {
        var totalGain = 0;
        var container = document.getElementById('whatIfContainer');
        if (!container) return;
        container.querySelectorAll('.sim-check:checked').forEach(function(cb) {
            totalGain += parseFloat(cb.dataset.gain);
        });
        var projected = Math.min(100, Math.round(this.originalScore + totalGain));
        var grade = this.getGradeLabel(projected);

        var scoreEl = document.getElementById('projectedScore');
        var gradeEl = document.getElementById('projectedGrade');
        var barEl = document.getElementById('simProgressBar');

        if (scoreEl) {
            scoreEl.textContent = projected;
            scoreEl.className = 'fw-bold text-' + this.getScoreColor(projected);
        }
        if (gradeEl) {
            gradeEl.textContent = grade.grade + ' - ' + grade.label;
            gradeEl.className = 'badge bg-' + this.getScoreColor(projected).replace('green', 'success').replace('orange', 'warning');
        }
        if (barEl) {
            barEl.style.width = projected + '%';
            barEl.className = 'progress-bar ' + (projected >= 70 ? 'bg-success' : projected >= 50 ? 'bg-warning' : 'bg-danger');
        }
    },

    getScoreColor: function(s) {
        if (s >= 70) return 'green';
        if (s >= 50) return 'orange';
        return 'red';
    },

    getGradeLabel: function(s) {
        if (s >= 90) return { grade: 'A+', label: 'Exceptional Match' };
        if (s >= 80) return { grade: 'A', label: 'Strong Match' };
        if (s >= 70) return { grade: 'B+', label: 'Good Match' };
        if (s >= 60) return { grade: 'B', label: 'Moderate Match' };
        if (s >= 50) return { grade: 'C', label: 'Partial Match' };
        if (s >= 40) return { grade: 'D', label: 'Weak Match' };
        return { grade: 'F', label: 'Poor Match' };
    }
};

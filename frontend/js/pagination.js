/**
 * Reusable Pagination Component
 * Usage: Pagination.render(containerId, currentPage, totalPages, onPageChange)
 */
var Pagination = {
    render: function(containerId, page, totalPages, callback) {
        var container = document.getElementById(containerId);
        if (!container || totalPages <= 1) { if (container) container.innerHTML = ''; return; }
        var html = '<div class="pagination-wrapper">';
        html += '<button class="page-btn" ' + (page === 0 ? 'disabled' : '') + ' data-page="' + (page - 1) + '"><i class="fa-solid fa-chevron-left"></i></button>';
        var start = Math.max(0, page - 2);
        var end = Math.min(totalPages - 1, page + 2);
        if (start > 0) { html += '<button class="page-btn" data-page="0">1</button>'; if (start > 1) html += '<span class="px-2">...</span>'; }
        for (var i = start; i <= end; i++) {
            html += '<button class="page-btn' + (i === page ? ' active' : '') + '" data-page="' + i + '">' + (i + 1) + '</button>';
        }
        if (end < totalPages - 1) { if (end < totalPages - 2) html += '<span class="px-2">...</span>'; html += '<button class="page-btn" data-page="' + (totalPages - 1) + '">' + totalPages + '</button>'; }
        html += '<button class="page-btn" ' + (page === totalPages - 1 ? 'disabled' : '') + ' data-page="' + (page + 1) + '"><i class="fa-solid fa-chevron-right"></i></button>';
        html += '</div>';
        container.innerHTML = html;
        container.querySelectorAll('.page-btn:not([disabled])').forEach(function(btn) {
            btn.addEventListener('click', function() { callback(parseInt(this.dataset.page)); });
        });
    }
};

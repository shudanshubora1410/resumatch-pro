/**
 * Skeleton Loader Component
 * Usage: Skeleton.show(containerId, type, count)
 * Types: 'card', 'table-row', 'text', 'chart'
 */
var Skeleton = {
    show: function(containerId, type, count) {
        var container = document.getElementById(containerId);
        if (!container) return;
        count = count || 3;
        var html = '';
        switch (type) {
            case 'card':
                for (var i = 0; i < count; i++) {
                    html += '<div class="card mb-3"><div class="card-body"><div class="skeleton skeleton-text" style="width:70%"></div><div class="skeleton skeleton-text-sm"></div><div class="skeleton skeleton-text-sm" style="width:40%"></div></div></div>';
                }
                break;
            case 'table-row':
                html = '<div class="table-responsive"><table class="table"><tbody>';
                for (var i = 0; i < count; i++) {
                    html += '<tr><td><div class="skeleton skeleton-text" style="width:60%"></div></td><td><div class="skeleton skeleton-text" style="width:40%"></div></td><td><div class="skeleton skeleton-text" style="width:80px"></div></td><td><div class="skeleton skeleton-text" style="width:100px"></div></td></tr>';
                }
                html += '</tbody></table></div>';
                break;
            case 'text':
                for (var i = 0; i < count; i++) {
                    html += '<div class="skeleton skeleton-text" style="width:' + (90 - i * 15) + '%"></div>';
                }
                break;
            case 'chart':
                html = '<div class="skeleton" style="height:250px;border-radius:12px;"></div>';
                break;
        }
        container.innerHTML = html;
    },
    hide: function(containerId) {
        var container = document.getElementById(containerId);
        if (container) container.innerHTML = '';
    }
};

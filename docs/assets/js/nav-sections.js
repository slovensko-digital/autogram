// docmd renders collapsible sidebar sections as plain <span>s that only react to mouse clicks.
// Make them buttons: focusable, operable with Enter and Space, with aria-expanded on the control.
(function () {
    var LABELS = '.sidebar-nav li.collapsible > .nav-label';

    function sync(label) {
        label.setAttribute('aria-expanded', label.parentElement.classList.contains('expanded') ? 'true' : 'false');
    }

    function enhance() {
        document.querySelectorAll('.sidebar-nav a').forEach(function (link) {
            if (link.classList.contains('active')) link.setAttribute('aria-current', 'page');
            else link.removeAttribute('aria-current');
        });

        document.querySelectorAll(LABELS).forEach(function (label) {
            if (!label.hasAttribute('role')) {
                label.setAttribute('role', 'button');
                label.setAttribute('tabindex', '0');
            }
            sync(label);
        });
    }

    document.addEventListener('keydown', function (e) {
        if (e.key !== 'Enter' && e.key !== ' ') return;
        var label = e.target.closest && e.target.closest(LABELS);
        if (!label) return;
        e.preventDefault();
        label.click();
    });

    // docmd toggles the section on the same click and replaces the sidebar on SPA navigation
    new MutationObserver(enhance).observe(document.documentElement, {
        subtree: true, childList: true, attributes: true, attributeFilter: ['class']
    });

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', enhance);
    else enhance();

    // docmd centres the current page's link in the sidebar 100 ms after loading, so the menu jumped on every page,
    // even when the link was in view. Bring the link into view right away and only when it is hidden, and ignore
    // docmd's centring then.
    (function keepSidebarStill() {
        var nav = document.querySelector('.sidebar-nav');
        if (!nav) return;

        function isHidden(link) {
            var box = nav.getBoundingClientRect();
            var rect = link.getBoundingClientRect();
            return rect.top < box.top || rect.bottom > box.bottom;
        }

        var link = nav.querySelector('a.active');
        if (link && isHidden(link)) {
            var top = link.getBoundingClientRect().top - nav.getBoundingClientRect().top;
            nav.scrollTop += top - (nav.clientHeight - link.offsetHeight) / 2;
        }

        var scrollTo = nav.scrollTo;
        nav.scrollTo = function () {
            var active = nav.querySelector('a.active');
            if (active && !isHidden(active)) return;
            return scrollTo.apply(nav, arguments);
        };
    })();
})();

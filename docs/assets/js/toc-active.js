// docmd marks the table-of-contents link of the heading that crosses a narrow band near the top of the window. A heading
// reached through its link stops above that band, and the headings of the last sections never get there, so clicking a
// section in the table of contents did not make it active. This script decides the active link itself:
// - the section the user jumped to through a link, until they scroll the page themselves,
// - otherwise the last heading that has scrolled up to the top, or the last one at the end of the page.
(function () {
    var pinnedId = null;
    var jumping = false;
    var jumpTimer = null;
    var path = location.pathname;
    var frame = null;

    function idOf(href) {
        try {
            return decodeURIComponent((href || '').replace(/^[^#]*#/, ''));
        } catch (e) {
            return '';
        }
    }

    function sections() {
        var result = [];
        document.querySelectorAll('.toc-link').forEach(function (link) {
            var heading = document.getElementById(idOf(link.getAttribute('href')));
            if (heading) result.push({ link: link, heading: heading });
        });
        return result;
    }

    function activeId(list) {
        if (pinnedId && document.getElementById(pinnedId)) return pinnedId;

        var root = document.documentElement;
        if (window.scrollY > 0 && window.innerHeight + window.scrollY >= root.scrollHeight - 2)
            return list[list.length - 1].heading.id;

        // a heading counts as reached where its link scrolls it to (its scroll-margin-top)
        var id = list[0].heading.id;
        list.forEach(function (section) {
            var offset = parseFloat(getComputedStyle(section.heading).scrollMarginTop) || 0;
            if (section.heading.getBoundingClientRect().top <= offset + 8) id = section.heading.id;
        });
        return id;
    }

    function keepVisible(link) {
        var container = document.querySelector('.toc-sidebar');
        if (!container || container.scrollHeight <= container.clientHeight) return;
        var box = container.getBoundingClientRect();
        var rect = link.getBoundingClientRect();
        if (rect.top < box.top) container.scrollTop -= box.top - rect.top + 8;
        else if (rect.bottom > box.bottom) container.scrollTop += rect.bottom - box.bottom + 8;
    }

    function apply() {
        frame = null;
        var list = sections();
        if (!list.length) return;
        var id = activeId(list);
        list.forEach(function (section) {
            var active = section.heading.id === id;
            if (section.link.classList.contains('active') !== active) {
                section.link.classList.toggle('active', active);
                if (active) keepVisible(section.link);
            }
        });
    }

    function schedule() {
        if (frame === null) frame = requestAnimationFrame(apply);
    }

    // scrolling caused by a link (smooth, a few hundred ms) keeps the jumped-to section, any later scrolling releases it
    function jumpTo(id, settleMs) {
        pinnedId = id || null;
        jumping = true;
        settle(settleMs);
        schedule();
    }

    function settle(ms) {
        clearTimeout(jumpTimer);
        jumpTimer = setTimeout(function () { jumping = false; }, ms);
    }

    window.addEventListener('scroll', function () {
        if (jumping) settle(200);
        else pinnedId = null;
        schedule();
    }, { passive: true });

    window.addEventListener('resize', schedule);

    document.addEventListener('click', function (e) {
        var link = e.target.closest && e.target.closest('a[href*="#"]');
        if (!link) return;
        var url = new URL(link.href, location.href);
        if (url.pathname === location.pathname && url.hash) jumpTo(idOf(url.hash), 300);
    });

    window.addEventListener('popstate', function () {
        if (location.pathname === path) jumpTo(idOf(location.hash), 300);
    });

    // docmd scrolls to the hash shortly after loading and after SPA navigation, and moves the active class while
    // scrolling; a changed page starts with its own hash, a moved class is put back
    new MutationObserver(function () {
        if (location.pathname !== path) {
            path = location.pathname;
            jumpTo(idOf(location.hash), 800);
        }
        schedule();
    }).observe(document.body, { subtree: true, childList: true, attributes: true, attributeFilter: ['class'] });

    jumpTo(idOf(location.hash), 800);
})();

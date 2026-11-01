// The top banner (the edge version's notice) spans the whole window above the sidebar, so branding.css moves the sidebar,
// the sticky header and the page down by its height, which changes with the window width and the language.
(function () {
    var banner = document.querySelector('.docmd-banner--pos-top');
    if (!banner) return;
    new ResizeObserver(function () {
        document.documentElement.style.setProperty('--top-banner-h', banner.offsetHeight + 'px');
    }).observe(banner);
})();

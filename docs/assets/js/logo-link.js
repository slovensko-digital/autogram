// docmd links the logo to the home page with a path relative to the current page ("./", "../"). On SPA navigation it
// replaces the page content and the sidebar links, but not the logo, so after moving from the home page to a subpage the
// logo pointed to that subpage and the first click only reloaded it. Resolve the link once on load to an absolute path,
// which stays right on every page.
(function () {
    document.querySelectorAll('.sidebar-header a.logo-link').forEach(function (link) {
        link.setAttribute('href', new URL(link.href).pathname);
    });
})();

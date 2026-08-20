(() => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (window.htmx && csrfToken && csrfHeader) {
        document.body.addEventListener("htmx:configRequest", (event) => {
            event.detail.headers[csrfHeader] = csrfToken;
        });
    }
})();

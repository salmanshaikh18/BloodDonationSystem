/*
 * Small helpers for the pages. Plain JavaScript, no libraries.
 *
 * Ask before an action that can't be undone. Add data-confirm to the form:
 *   <form method="post" data-confirm="Delete this donor?"> ... </form>
 */
document.addEventListener("submit", (event) => {
    const message = event.target.dataset.confirm;
    if (message && !window.confirm(message)) {
        event.preventDefault();
    }
});

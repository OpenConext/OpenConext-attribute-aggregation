//sneaky, but alternative is redux and that is overkill
export let backPage = null;

export function setBackPage(page) {
    backPage = page;
}

export function clearBackPage() {
    backPage = null;
}

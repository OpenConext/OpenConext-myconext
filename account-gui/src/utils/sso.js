export const proceed = magicLinkUrlPrefix => {
    const urlSearchParams = new URLSearchParams(window.location.search);
    const redirect = decodeURIComponent(urlSearchParams.get("redirect"));
    //Ensure we are not attacked by an open redirect
    if (redirect.startsWith(magicLinkUrlPrefix)) {
        const hash = urlSearchParams.get('h');
        window.location.href = `${redirect}?h=${hash}`;
    } else {
        throw new Error("Invalid redirect: " + redirect);
    }
};

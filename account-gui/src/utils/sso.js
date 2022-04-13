export const proceed = magicLinkUrlPrefix => {
    const urlSearchParams = new URLSearchParams(window.location.search);
    const redirect = decodeURIComponent(urlSearchParams.get("redirect"));
    //Ensure we are not attacked by an open redirect
    if (redirect.startsWith(magicLinkUrlPrefix)) {
        window.location.href = `${redirect}?h=${urlSearchParams.get('h')}`;
    } else {
        throw new Error("Invalid redirect: " + redirect);
    }
};

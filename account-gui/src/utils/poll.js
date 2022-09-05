export const poll = async ({ fn, validate, interval, maxAttempts }) => {
    let attempts = 0;

    const executePoll = async (resolve, reject) => {
        const result = await fn();
        attempts++;

        if (validate(result)) {
            return resolve(result);
        } else if (maxAttempts && attempts === maxAttempts) {
            return reject(new Error('Exceeded max attempts'));
        } else {
            setTimeout(executePoll, interval, resolve, reject);
        }
    };

    return new Promise(executePoll);
};

export const suspensionMinutes = suspendUntilEpochMilliseconds => {
    const epochNow = new Date().getTime();
    const milliDiff = suspendUntilEpochMilliseconds - epochNow;
    const minuteDiff = Math.round(milliDiff / 1000 / 60);
    return minuteDiff < 1 ? 0 : minuteDiff;
}

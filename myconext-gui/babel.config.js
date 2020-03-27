module.exports = {
    presets: [
        [
            '@babel/preset-env',
            {
                "modules": false,
                "corejs": 3,
                "useBuiltIns": "usage",
                "targets": {
                    "browsers": [
                        "> 0.5%",
                        "last 2 major versions",
                        "safari >= 9",
                        "not ie < 11",
                        "not dead"
                    ]
                }
            }],
    ]
};
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require('path');

const mode = process.env.NODE_ENV || 'development';
const prod = mode === 'production';

module.exports = {
    entry: {
        bundle: ['./src/main.js']
    },
    resolve: {
        extensions: ['.mjs', '.js', '.svelte', '.tsx', '.ts'],
        mainFields: ['svelte', 'browser', 'module', 'main']
    },
    output: {
        path: __dirname + '/public',
        filename: '[name].[hash].js',
        chunkFilename: '[name].[hash].js',
        publicPath: '/',
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.m?js$/,
                use: {
                    loader: 'babel-loader'
                },
                exclude: /\bcore-js\b/,
            },
            {
                test: /\.svelte$/,
                use: [
                    {
                        loader: 'babel-loader',
                    },
                    {
                        loader: 'svelte-loader',
                        options: {
                            preprocess: require('svelte-preprocess')({
                                postcss: true
                            }),
                            emitCss: true,
                            accessors: true,
                            dev: true,
                            onwarn: (warning, handler) => {
                                if (warning.code.toLowerCase().startsWith('a11y')) {
                                    return
                                }
                                // Handle all other warnings normally
                                handler(warning)
                            },
                        },
                    }
                ],
            },
            {
                test: /\.css$/,
                use: [
                    /**
                     * MiniCssExtractPlugin doesn't support HMR.
                     * For developing, use 'style-loader' instead.
                     * */
                    prod ? MiniCssExtractPlugin.loader : 'style-loader',
                    'css-loader'
                ]
            },
            {
                test: /\.svg$/,
                loader: 'svg-inline-loader',
                options: {
                    removeSVGTagAttrs: false
                }
            }
        ]
    },
    mode,
    plugins: [
        new MiniCssExtractPlugin({
            filename: '[name].[hash].css'
        }),
        new HtmlWebpackPlugin({
            template: "src/index.html.ejs",
            favicon: "src/favicon.ico",
            hash: true
        }),
    ],
    devtool: prod ? false : 'source-map',
    devServer: {
        port: 3000,
        proxy:
            [
                {
                    context: ['/myconext/api'],
                    target: 'http://localhost:8081',
                },
                {
                    context: ['/config'],
                    target: 'http://localhost:8081',
                },
                {
                    context: ['/register'],
                    target: 'http://localhost:8081',
                },
                {
                    context: ['/tiqr'],
                    target: 'http://localhost:8081',
                }
            ],
        historyApiFallback: true
    }
};

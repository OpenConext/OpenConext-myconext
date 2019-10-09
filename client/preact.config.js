export default (config, env, helpers) => {
    config.devServer.proxy = [
        {
            path: '/surfid/api/**',
            target: 'http://localhost:8081/',
        }
    ];
}
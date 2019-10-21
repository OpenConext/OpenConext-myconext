export default (config, env, helpers) => {
    if (config.devServer) {
        config.devServer.proxy = [
            {
                path: '/surfid/api/**',
                target: 'http://localhost:8081/',
            }
        ];
    }
}
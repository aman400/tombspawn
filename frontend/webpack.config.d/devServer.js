config.devServer = Object.assign(
    {},
    config.devServer || {},
    { historyApiFallback: true }
)
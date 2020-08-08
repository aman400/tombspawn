config.module.rules.push({
    test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
    use: [
      {
        loader: 'file-loader',
        options: {
          name: '[name].[ext]',
          outputPath: 'fonts/'
        }
      }
    ]
});


config.module.rules.push({
    test: /\.(png|jpe?g|gif|svg|otf)$/,
    include: /images/,
    use: [
      {
        loader: 'file-loader',
      }
    ]
});
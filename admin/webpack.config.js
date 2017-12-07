var path = require('path');
var webpack = require('webpack');

module.exports = {
    entry: './src/main/js/app.js',
    devtool: 'sourcemaps',
    cache: true,

    output: {
        path: __dirname + "/src/main/resources/admin-web/build",
        filename: 'bundle.js',
        sourceMapFilename: '[name].js.map',
        publicPath: '/admin/build/'
    },

    devServer: {
        inline: true,
        port: 7000,
        contentBase: __dirname + "/src/main/resources/admin-web",
        historyApiFallback: {
            disableDotRule: true
        }
    },

    plugins: [
        new webpack.LoaderOptionsPlugin({
            debug: true
        }),
        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoEmitOnErrorsPlugin()
    ],

    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            },
            {
                test: /\.(jpeg|jpg|png|gif|svg)$/,
                loaders: ['url-loader?limit=8192', 'img-loader']
            },
            {
                test: /\.(scss|css)$/,
                loaders: ['style-loader', 'css-loader?sourceMap', 'sass-loader?sourceMap']
            },
            {
                test: /\.json$/,
                loaders: ['json-loader']
            },
            {
                test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
                loader: 'file-loader?mimetype=staticl.image/svg+xml'
            },
            {
                test: /\.woff(\?v=\d+\.\d+\.\d+)?$/,
                loader: "url-loader?mimetype=application/font-woff"
            },
            {
                test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/,
                loader: "url-loader?mimetype=application/font-woff"
            },
            {
                test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
                loader: "url-loader?mimetype=application/octet-stream"
            },
            {
                test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
                loader: "file-loader"
            }
        ]
    },

    resolve: {
        extensions: ['.js', '.jsx', '.json']
    }
};
# 预处理器 - 通过本地文件缓存最大幅加速 npm 安装
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
# 使用淘宝源或仅依赖标准源并在容器内忽略不可用情况
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# SPA路由回退
RUN echo 'server { listen 80; location / { root /usr/share/nginx/html; index index.html; try_files $uri $uri/ /index.html; } }' > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

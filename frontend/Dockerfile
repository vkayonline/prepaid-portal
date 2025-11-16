# 1. Build Stage
FROM node:20-alpine AS builder

ENV PNPM_HOME="/pnpm" \
    PATH="$PNPM_HOME:$PATH"
RUN corepack enable

WORKDIR /app

COPY package.json pnpm-lock.yaml ./

# Download dependencies
RUN pnpm fetch

# Copy source code
COPY . .

# Build application
RUN pnpm install --offline && pnpm build

# 2. Runtime Stage - Nginx
FROM nginx:stable-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]


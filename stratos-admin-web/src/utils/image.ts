export function imageUrl(path?: string | null) {
  if (!path || path === '/placeholder.svg') return ''
  if (path.startsWith('http') || path.startsWith('data:') || path.startsWith('/api/')) return path
  if (path.startsWith('/files/')) return `/api${path}`
  return path
}

export function hasProductImage(path?: string | null) {
  return Boolean(imageUrl(path))
}

export function parseAlbum(imageList?: string | null, mainImage?: string | null) {
  return (imageList || '')
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item && item !== mainImage)
}

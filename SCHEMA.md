# Processing URI Schema Definition

## Local File Schema
```
pde:///path/to/sketch.pde
```

## Sketch Operations

### Create New Sketch
```
pde://sketch/new
```

### Load Base64 Encoded Sketch
```
pde://sketch/base64/<base64_encoded_content>
```
Optional query parameters:
- `data`: Comma-separated URLs of data files to download
- `code`: Comma-separated URLs of code files to download
- `pde`: Comma-separated URLs of Processing sketch files to download
- `mode`: Processing mode identifier

### Load Sketch from URL
```
pde://sketch/url/domain.com/path/to/sketch.pde
```
Supports the same query parameters as base64 endpoint.
Optional query parameters:

- `data`: Comma-separated URLs of data files to download
- `code`: Comma-separated URLs of code files to download
- `pde`: Comma-separated URLs of Processing sketch files to download
- `mode`: Processing mode identifier

Example with query parameters:
```
pde://sketch/url/example.com/sketch.pde?data=image1.png,sound1.mp3&mode=java
```

## Preferences
```
pde://preferences?key1=value1&key2=value2
```
Sets and saves multiple preferences in a single operation.

## Security Considerations
- URL-based operations automatically prepend https:// if no scheme is provided
- All URLs and query parameters are decoded using UTF-8
- File downloads occur asynchronously in a separate thread
- Base64 and remote sketches are saved to temporary folders
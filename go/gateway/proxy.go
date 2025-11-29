package gateway

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/gin-gonic/gin"
)


func ReverseProxy(target string) gin.HandlerFunc {
	return func(c *gin.Context) {
		remoto, err := url.Parse(target)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "URL de destino inválida"})
			return
		}

		proxy := httputil.NewSingleHostReverseProxy(remoto)
		fmt.Println("Redirigiendo a:", remoto.String())
		c.Request.URL.Path = c.Param("path")
		c.Request.URL.Host = remoto.Host
		c.Request.URL.Scheme = remoto.Scheme
		c.Request.Host = remoto.Host
		proxy.ServeHTTP(c.Writer, c.Request)
	}
}

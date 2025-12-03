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

		path := c.Param("path")

		c.Request.URL.Path = path

		proxy.ErrorHandler = func(rw http.ResponseWriter, req *http.Request, err error) {
			c.JSON(http.StatusBadGateway, gin.H{"error": "Error de conexión con el microservicio", "detalle": err.Error()})
		}

		proxy.ServeHTTP(c.Writer, c.Request)
	}
}

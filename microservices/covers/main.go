package main

import (
	"os"
	"net/http"
	"log"
	"strings"
	"github.com/opentracing/opentracing-go"
	"github.com/uber/jaeger-client-go/config"
	"github.com/opentracing/opentracing-go/ext"
	"io"
)

var tracer opentracing.Tracer

func main() {
	var closer io.Closer
	var err error
	tracer, closer, err = config.Configuration{
		ServiceName: "covers",
	}.NewTracer()

	defer closer.Close()
	if err != nil {
		log.Fatal(err)
		os.Exit(3)
	}

	http.HandleFunc("/covers/", covers)
	log.Fatal(http.ListenAndServe(":8082", nil))
}

func covers(w http.ResponseWriter, r *http.Request) {

	span := trace(r)
	defer span.Finish()

	id := strings.TrimPrefix(r.URL.Path, "/covers/")
	log.Print("Covers for book: ", id)

	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte("[{\"id\" : 1, \"url\" : \"https://upload.wikimedia.org/wikipedia/en/e/e4/Ender%27s_game_cover_ISBN_0312932081.jpg\"}]"))
}

func trace(r *http.Request) opentracing.Span {
	wireContext, err := tracer.Extract(
		opentracing.HTTPHeaders,
		opentracing.HTTPHeadersCarrier(r.Header))

	var span opentracing.Span
	if err != nil {
		log.Println("Spancontext not found in carrier")
		span = tracer.StartSpan("listCovers")
	} else {
		log.Println("Using existing span")
		span = tracer.StartSpan(
			"listCovers",
			ext.RPCServerOption(wireContext))
	}
	return span
}
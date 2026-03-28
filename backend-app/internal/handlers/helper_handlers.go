package handlers

import (
	"context"
	"encoding/json"
	"errors"
	"groupeak/internal/middleware"
	"io"
	"net/http"
	"strconv"

	"github.com/go-chi/chi/v5"
)

func WriteJSON(w http.ResponseWriter, status int, data interface{}) error {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	return json.NewEncoder(w).Encode(data)
}

func WriteError(w http.ResponseWriter, status int, msg string) {
	_ = WriteJSON(w, status, map[string]string{"error": msg})
}

func DecodeJSON(w http.ResponseWriter, r *http.Request, dst interface{}) error {
	r.Body = http.MaxBytesReader(w, r.Body, maxJSONBody)
	dec := json.NewDecoder(r.Body)
	dec.DisallowUnknownFields()

	if err := dec.Decode(dst); err != nil {
		return err
	}

	if err := dec.Decode(&struct{}{}); err != io.EOF {
		return errors.New("request body must constain only one JSON object")
	}

	return nil
}

func GetUserIDFromContext(ctx context.Context) (int64, bool) {
	v := ctx.Value(middleware.UserIDKey)
	id, ok := v.(int64)
	return id, ok
}

// ParseIDParam — общий парсер числовых URL-параметров
func ParseIDParam(r *http.Request, name string) (int64, error) {
	v := chi.URLParam(r, name)
	id, err := strconv.ParseInt(v, 10, 64)
	if err != nil || id <= 0 {
		return 0, errors.New("invalid id")
	}
	return id, nil
}

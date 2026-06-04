import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      let message = 'An unexpected error occurred';
      if (err.error?.message) message = err.error.message;
      else if (err.status === 0)   message = 'Network error – please check your connection';
      else if (err.status === 403) message = 'You do not have permission to perform this action';
      else if (err.status === 404) message = 'The requested resource was not found';
      else if (err.status === 500) message = 'Server error – please try again later';
      return throwError(() => ({ ...err, userMessage: message }));
    })
  );
};

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Appointment } from '../appointment-list/appointment-list.component';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import dayjs from 'dayjs';

@Injectable({ providedIn: 'root' })
export class WorkshopService {
    constructor(
        private http: HttpClient,
    ) {}
    findAppointments(from: dayjs.Dayjs, to: dayjs.Dayjs, address?: string, car?: string): Observable<HttpResponse<Appointment[]>> {
        const params = this.createRequestOption({
            from: from.toJSON(),
            to: to.toJSON(),
            address,
            car,
        });

        return this.http.get<Appointment[]>(`api/workshops`, { observe: 'response', params });
    }

    bookAppointment(uuid: string, name: string, contactInfo: string): Observable<HttpResponse<Appointment>> {
        const params = this.createRequestOption({
            uuid,
            name,
            contactInfo,
        });

        return this.http.put<Appointment>(`api/workshops`, null, { observe: 'response', params });
    }

    createRequestOption = (req: any): HttpParams => {
        let options: HttpParams = new HttpParams();
        if (req) {
            Object.keys(req).forEach(key => {
                if (req[key]) {
                    options = options.set(key, req[key]);
                }
            });
        }
        return options;
    };
}

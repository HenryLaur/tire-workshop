import { Component, OnInit } from '@angular/core';
import dayjs from 'dayjs';
import { WorkshopService } from '../service/workshop.service';
import { catchError, finalize, of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

export interface Appointment {
    name: string;
    address: string;
    cars: string[];
    time: string;
    clicked?: boolean;
    uuid: string;
}

@Component({
    selector: 'app-appointment-list',
    templateUrl: './appointment-list.component.html',
    styleUrls: ['./appointment-list.component.scss'],
})
export class AppointmentListComponent implements OnInit {
    appointments: Appointment[] = [];
    car: string = '';
    address: string = '';
    from: Date = dayjs().toDate();
    to: Date = dayjs().add(1, 'year').toDate();
    isModalOpen = false;
    isLoading = false;
    contactInfo = '';
    selectedWorkshop?: Appointment;
    error?: { status: string; message: string };

    constructor(private workshopService: WorkshopService) {}

    ngOnInit(): void {
        this.search();
    }

    public search(): void {
        this.isLoading = true;
        this.workshopService
            .findAppointments(dayjs(this.from), dayjs(this.to), this.address, this.car)
            /* Added timeout so we can see cool loading effect, otherwise its too fast :D */
            .pipe(
                finalize(() => setTimeout(() => (this.isLoading = false), 1000)),
                catchError(httpError => {
                    this.error = { status: httpError.status, message: httpError.error?.message };
                    return of();
                }),
            )
            .subscribe(response => {
                this.appointments = response.body!;
                delete this.error;
            });
    }

    public bookAppointment(): void {
        this.isLoading = true;
        this.workshopService
            .bookAppointment(this.selectedWorkshop!.uuid, this.selectedWorkshop!.name, this.contactInfo)
            /* Added timeout so we can see cool loading effect, otherwise its too fast :D */
            .pipe(
                finalize(() => setTimeout(() => (this.isLoading = false), 1000)),
                catchError(httpError => {
                    this.error = { status: httpError.error.status, message: httpError.error.message };
                    return of();
                }),
            )
            .subscribe(() => {
                this.search();
            });
    }
}

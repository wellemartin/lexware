import {Component, inject, OnInit, signal, WritableSignal} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {MatDatepicker, MatDatepickerModule, MatDatepickerToggle} from '@angular/material/datepicker';
import {FormsModule} from '@angular/forms';
import {MatNativeDateModule} from '@angular/material/core';
import {MatButton} from '@angular/material/button';

interface WorkingTime {
  name: string;
  date: Date;
  startTime: string;
  endTime: string;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    FormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    MatDatepicker,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDatepickerToggle, MatButton, MatSuffix],
  templateUrl: './app.html',
  standalone: true,
  styleUrl: './app.scss'
})
export class App implements OnInit{
  private httpClient = inject(HttpClient);
  selectedDate?: Date;
  startTime?: string;
  endTime?: string;
  name?: string;
  times: WritableSignal<WorkingTime[]> = signal<WorkingTime[]>([]);

  sendData() {
    this.httpClient.post('http://localhost:8080/working-time', {
      name: this.name,
      date: new Date(this.selectedDate!.setHours(12)),
      startTime: this.startTime,
      endTime: this.endTime
    }).subscribe({
      next: res => {
        console.log('Erfolg:', res);
        this.loadData()
      },
      error: err => {
        console.error('Fehler:', err)
        if((err as HttpResponse<any>).status === 404) {
          alert('Der Mitarbeiter wurde nicht gefunden. Bitte überprüfen Sie den Namen.');
        } else if ((err as HttpResponse<any>).status === 400) {
          alert('Die Daten sind ungültig. Bitte überprüfen Sie die Eingaben.');
        } else if ((err as HttpResponse<any>).status === 409) {
          alert('Für diesen Mitarbeiter existiert bereits ein Eintrag für diesen Tag. Bitte überprüfen Sie die Eingaben.');
        }
      },
      complete: () => {
        this.name = undefined;
        this.selectedDate = undefined;
        this.startTime = undefined;
        this.endTime = undefined;
      }
    });
  }

  ngOnInit(): void {
    this.loadData()
  }

  loadData() {
    this.httpClient.get<WorkingTime[]>('http://localhost:8080/working-time').subscribe({
      next: (data) => {
        this.times.set(data);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Daten:', err);
      }
    });
  }

  protected readonly JSON = JSON;
}

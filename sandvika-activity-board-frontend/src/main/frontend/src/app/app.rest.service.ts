import {Injectable}    from '@angular/core';
import {Http, Response} from '@angular/http';

import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';

import {Athlete} from './athlete';
import {Activity} from "./activities";

@Injectable()
export class AppRestService {
    // private restUrl = 'http://localhost:8005/';  // URL to web api
    private restUrl = '';  // URL to web api

    constructor(private http: Http) {
    }

    getLeaderBoardMonthPoints(): Observable<Athlete[]> {
    return this.http.get(this.restUrl + "leaderboard/month/points")
        .map(this.extractData)
        .catch(this.handleError);
    }

    getAthleteByLastName(lastName): Observable<Activity[]> {
        return this.http.get(this.restUrl + "athlete/" + lastName + "/activities")
            .map(this.extractData)
            .catch(this.handleError);
    }

    private extractData(res: Response) {
        let body = res.json();
        return body || { };
    }

    private handleError (error: Response | any) {
        // In a real world app, we might use a remote logging infrastructure
        let errMsg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            const err = body.error || JSON.stringify(body);
            errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Promise.reject(errMsg);
    }
}
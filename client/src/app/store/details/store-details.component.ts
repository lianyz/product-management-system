import {Component, OnInit} from "@angular/core";
import {Store} from "../../model/store";
import {CommonService} from "../../common/common.service";
import {api} from "../../constants/api";
import {ActivatedRoute, Router} from "@angular/router";
@Component({
    selector: 'store-details-component',
    templateUrl: 'store-details.component.html'
})
export class StoreDetailsComponent implements OnInit {
    selectedStore: Store;

    constructor(private storeService: CommonService, private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.loadData();
    }

    getImageUrl(id: string): string {
        return api.SERVER + 'image/' + id;
    }

    private loadData(): void {
        this.storeService.loadByIdUnauthorized(api.STORE, this.route.snapshot.params['id'])
            .subscribe(
                store => this.selectedStore = store,
                err => this.logError(err));
    }

    private logError(err: Error): void {
        console.error('There was an error: ' + err);
        this.router.navigate(['/']);
    }
}
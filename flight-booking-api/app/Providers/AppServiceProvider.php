<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;
use Illuminate\Console\Scheduling\Schedule;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        // Schedule task để tự động hủy vé hết hạn
        $this->app->booted(function () {
            $schedule = $this->app->make(Schedule::class);
            // Chạy mỗi 5 phút để kiểm tra và hủy các đặt vé hết hạn
            $schedule->command('bookings:cancel-expired')->everyFiveMinutes();
        });
    }
}

<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::table('dat_ve', function (Blueprint $table) {
            $table->string('ma_giao_dich', 50)->nullable()->after('tong_tien');
            $table->dateTime('thoi_gian_thanh_toan')->nullable()->after('ma_giao_dich');
        });
    }

    public function down()
    {
        Schema::table('dat_ve', function (Blueprint $table) {
            $table->dropColumn(['ma_giao_dich', 'thoi_gian_thanh_toan']);
        });
    }
};

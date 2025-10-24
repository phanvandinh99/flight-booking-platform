<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('gia_ve', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('ma_chuyen_bay');
            $table->enum('hang_ve', ['pho_thong', 'thuong_gia', 'hang_nhat']);
            $table->decimal('gia', 12, 2);
            $table->string('hanh_ly_ky_gui')->default('20kg');
            $table->text('chinh_sach_huy_ve')->nullable();
            $table->text('chinh_sach_doi_ve')->nullable();
            $table->date('ngay_bat_dau');
            $table->date('ngay_ket_thuc');
            $table->timestamps();

            $table->foreign('ma_chuyen_bay')->references('id')->on('chuyen_bay')->onDelete('cascade');
            $table->unique(['ma_chuyen_bay', 'hang_ve', 'ngay_bat_dau']);
        });
    }

    public function down()
    {
        Schema::dropIfExists('gia_ve');
    }
};

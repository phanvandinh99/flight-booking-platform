<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('dat_ve', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('ma_khach_hang');
            $table->unsignedBigInteger('ma_chuyen_bay');
            $table->string('ma_dat_ve', 20)->unique(); // ABC123
            $table->enum('trang_thai', ['giu_cho', 'da_thanh_toan', 'da_huy'])->default('giu_cho');
            $table->dateTime('thoi_gian_het_han_giu_cho')->nullable();
            $table->decimal('tong_tien', 12, 2);
            $table->timestamps();

            $table->foreign('ma_khach_hang')->references('id')->on('nguoi_dung')->onDelete('cascade');
            $table->foreign('ma_chuyen_bay')->references('id')->on('chuyen_bay')->onDelete('cascade');
        });
    }

    public function down()
    {
        Schema::dropIfExists('dat_ve');
    }
};

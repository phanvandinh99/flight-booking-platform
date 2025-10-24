<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('chuyen_bay', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('ma_hang_hang_khong');
            $table->unsignedBigInteger('ma_may_bay');
            $table->string('ma_chuyen_bay'); // VN123
            $table->unsignedBigInteger('ma_tuyen_bay');
            $table->dateTime('gio_khoi_hanh');
            $table->dateTime('gio_ha_canh');
            $table->string('tan_suat'); // 'hang_ngay', 'thu_2_thu_4', v.v.
            $table->enum('trang_thai', ['du_kien', 'bi_huy', 'da_hoan_thanh'])->default('du_kien');
            $table->timestamps();

            $table->foreign('ma_hang_hang_khong')->references('id')->on('hang_hang_khong')->onDelete('cascade');
            $table->foreign('ma_may_bay')->references('id')->on('may_bay')->onDelete('cascade');
            $table->foreign('ma_tuyen_bay')->references('id')->on('tuyen_bay')->onDelete('cascade');
            $table->index(['ma_chuyen_bay', 'gio_khoi_hanh']);
        });
    }

    public function down()
    {
        Schema::dropIfExists('chuyen_bay');
    }
};

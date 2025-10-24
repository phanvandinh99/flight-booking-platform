<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('hanh_khach', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('ma_dat_ve');
            $table->string('ho_ten');
            $table->string('so_ho_chieu')->nullable();
            $table->string('so_ghe')->nullable();
            $table->enum('hang_ve', ['pho_thong', 'thuong_gia', 'hang_nhat']);
            $table->timestamps();

            $table->foreign('ma_dat_ve')->references('id')->on('dat_ve')->onDelete('cascade');
        });
    }

    public function down()
    {
        Schema::dropIfExists('hanh_khach');
    }
};

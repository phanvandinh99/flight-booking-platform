<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('may_bay', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('ma_hang_hang_khong');
            $table->string('loai_may_bay'); // Airbus A320
            $table->integer('tong_so_ghe');
            $table->json('so_do_ghe')->nullable(); // lưu sơ đồ ghế dưới dạng JSON
            $table->timestamps();

            $table->foreign('ma_hang_hang_khong')
                  ->references('id')
                  ->on('hang_hang_khong')
                  ->onDelete('cascade');
        });
    }

    public function down()
    {
        Schema::dropIfExists('may_bay');
    }
};

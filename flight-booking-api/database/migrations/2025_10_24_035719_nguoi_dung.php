<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('nguoi_dung', function (Blueprint $table) {
            $table->id();
            $table->string('ten_day_du');
            $table->string('email')->unique();
            $table->string('so_dien_thoai')->nullable();
            $table->string('mat_khau');
            $table->enum('vai_tro', ['admin', 'dai_dien_hang', 'khach_hang']);
            $table->unsignedBigInteger('ma_hang_hang_khong')->nullable(); // chỉ đại diện hãng mới có
            $table->timestamp('da_xac_thuc_email')->nullable();
            $table->rememberToken();
            $table->timestamps();

            // Khóa ngoại (sẽ thêm sau khi bảng hang_hang_khong tồn tại)
            $table->foreign('ma_hang_hang_khong')
                  ->references('id')
                  ->on('hang_hang_khong')
                  ->onDelete('set null');
        });
    }

    public function down()
    {
        Schema::dropIfExists('nguoi_dung');
    }
};
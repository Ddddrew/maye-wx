<template>
	<view class="page">
		<checkbox-group @change="selected">
			<block v-for="dept in list" :key="dept.id">
				<view class="list-title">{{dept.deptName}}（{{ dept.count }}人）</view>
				<view class="item" v-for="member in dept.members" :key="member.userId">
					<view class="key">{{member.name}}</view>
					<checkbox class="value" :value="member.userId" :checked="member.checked"></checkbox>
				</view>
			</block>
		</checkbox-group>
	</view>
</template>

<script>
export default {
	data() {
		return {
			list: [
				{					id:1,					deptName:'管理部',					count:2,					members:[						{userId:10,name:'娜美',checked:true},						{userId:11,name:'王猛',checked:false},					]				},				{					id:2,					deptName:'技术部',					count:3,					members:[						{userId:12,name:'大猛子',checked:true},						{userId:13,name:'女流',checked:false},						{userId:14,name:'周淑怡',checked:false},					]				}
			],
			members: []
		};
	},
	onShow:function(){
		this.loadData(this)
	},
	onLoad:function(options){
		if(options.hasOwnProperty("members")){
			let members=options.members
			this.members=members.split(",")
			console.log(members)
		}
	},
	methods: {
		loadData:function(ref){
			ref.ajax(ref.url.searchUserGroupByDept,"POST",{keyword:ref.keyword},function(resp){
				let result=resp.data.result
				ref.list=result
				for(let dept of ref.list){
					for(let member of dept.members){
						if(ref.members.indexOf(member.userId+"")!=-1){
							member.checked=true
						}
						else{
							member.checked=false
						}
					}
				}
			})
		},
		selected:function(e){
			let that=this
			that.members=e.detail.value
			let pages=getCurrentPages()
			let prevPage=pages[pages.length-2]
			prevPage.members=that.members
			prevPage.finishMembers=true
		}
	}
};
</script>

<style lang="less">
@import url('members.less');
</style>
